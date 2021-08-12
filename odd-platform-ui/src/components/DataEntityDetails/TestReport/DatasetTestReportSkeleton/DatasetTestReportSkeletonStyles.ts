import { createStyles, Theme, WithStyles } from '@material-ui/core';

export const styles = (theme: Theme) =>
  createStyles({
    container: {},
    testReportSkeletonContainer: {
      margin: theme.spacing(0.25, 0, 4, 0),
      alignItems: 'center',
      flexWrap: 'nowrap',
    },
    testSkeletons: {
      flexWrap: 'nowrap',
      '& > *': {
        marginRight: theme.spacing(5),
      },
    },
    testCountSkeleton: {
      marginRight: theme.spacing(1),
      justifyContent: 'flex-end',
    },
  });

export type StylesType = WithStyles<typeof styles>;